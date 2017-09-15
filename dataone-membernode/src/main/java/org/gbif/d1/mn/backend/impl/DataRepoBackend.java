package org.gbif.d1.mn.backend.impl;

import org.gbif.api.model.common.DOI;
import org.gbif.api.model.common.paging.PagingRequest;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.vocabulary.IdentifierType;
import org.gbif.d1.mn.backend.Health;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.datarepo.api.DataRepository;
import org.gbif.datarepo.api.model.AlternativeIdentifier;
import org.gbif.datarepo.api.model.DataPackage;
import org.gbif.datarepo.api.model.FileInputContent;
import org.gbif.doi.metadata.datacite.DataCiteMetadata;
import org.gbif.doi.metadata.datacite.DataCiteMetadata.AlternateIdentifiers.AlternateIdentifier;
import org.gbif.doi.metadata.datacite.DataCiteMetadata.AlternateIdentifiers;
import org.gbif.doi.service.InvalidMetadataException;
import org.gbif.doi.service.datacite.DataCiteValidator;
import org.gbif.registry.doi.DoiType;
import org.gbif.registry.doi.registration.DoiRegistration;
import org.gbif.registry.doi.registration.DoiRegistrationService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.dataone.ns.service.exceptions.IdentifierNotUnique;
import org.dataone.ns.service.exceptions.InvalidSystemMetadata;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.ServiceFailure;
import org.dataone.ns.service.types.v1.Checksum;
import org.dataone.ns.service.types.v1.DescribeResponse;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.NodeReference;
import org.dataone.ns.service.types.v1.ObjectInfo;
import org.dataone.ns.service.types.v1.ObjectList;
import org.dataone.ns.service.types.v1.Permission;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of Member Node Backend supported on the GBIF Data Repo.
 */
public class DataRepoBackend implements MNBackend {

  public static final int MAX_PAGE_SIZE = 20;
  public static final String TAG_PREFIX = "DataOne";


  private static final Logger LOG = LoggerFactory.getLogger(DataRepoBackend.class);

  private static final String CHECKSUM_ALGORITHM  = "MD5";
  private static final String CONTENT_FILE  = "content";
  private static final String SYS_METADATA_FILE  = "system_metadata.xml";
  private static final JAXBContext JAXB_CONTEXT = getSystemMetadataJaxbContext();

  private final DataRepository dataRepository;
  private final DoiRegistrationService doiRegistrationService;
  private final DataRepoBackendConfiguration configuration;

  /**
   * Initializes a JAXBContext.
   */
  private static JAXBContext getSystemMetadataJaxbContext() {
    try {
      return JAXBContext.newInstance(SystemMetadata.class);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Gets the checksum of a data package.
   */
  private static Checksum dataPackageChecksum(DataPackage dataPackage) {
    return Checksum.builder().withValue(dataPackage.getChecksum()).withAlgorithm(CHECKSUM_ALGORITHM).build();
  }

  private static String toDataOneTag(String value) {
    return TAG_PREFIX + ':' + value;
  }

  /**
   * Converts a java.util.Date into a XMLGregorianCalendar.
   */
  private static XMLGregorianCalendar toXmlGregorianCalendar(Date date) {
    try {
      GregorianCalendar gregorianCalendar = new GregorianCalendar();
      gregorianCalendar.setTime(date);
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
    } catch (DatatypeConfigurationException ex) {
      LOG.error("Error converting date", ex);
      throw new ServiceFailure("Error reading data package date");
    }
  }

  /**
   *  Transforms a SystemMetadata object into a FileInputContent.
   */
  private static FileInputContent toFileContent(SystemMetadata sysmeta) throws InvalidSystemMetadata {
    try {
      StringWriter xmlMetadata = new StringWriter();
      JAXB_CONTEXT.createMarshaller().marshal(sysmeta, xmlMetadata);
      return new FileInputContent(SYS_METADATA_FILE, wrapInInputStream(xmlMetadata.toString()));
    } catch (JAXBException ex) {
      LOG.error("Error reading xml metadata", ex);
      throw new InvalidSystemMetadata("Error reading system metadata");
    }
  }

  /**
   *  Transforms a object into a FileInputContent.
   */
  private static FileInputContent toFileContent(InputStream object) throws JAXBException {
    return new FileInputContent(CONTENT_FILE, object);
  }

  /**
   * Translates a Identifier into a DataCite AlternateIdentifier.
   */
  private static Optional<AlternateIdentifier> toAlternateIdentifier(Identifier pid) {
    return Optional.ofNullable(pid).map(obsoleteIdentifier -> AlternateIdentifier.builder()
                                                                .withValue(obsoleteIdentifier.getValue())
                                                                .withAlternateIdentifierType(IdentifierType.UNKNOWN
                                                                                               .name())
                                                                .build());
  }

  /**
   *  Asserts that an pid exist, throws IdentifierNotUnique otherwise.
   */
  private void assertNotExists(Identifier pid) {
    if (dataRepository.getByAlternativeIdentifier(pid.getValue()).isPresent()) {
      throw new IdentifierNotUnique("Identifier already exists", pid.getValue());
    }
  }

  /**
   * Wraps a String in a ByteArrayInputStream.
   */
  private static InputStream wrapInInputStream(String in) {
    return new ByteArrayInputStream(in.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Creates DataCiteMetadata XML string out from the parameters provided.
   */
  private static String toDataRepoMetadata(DOI doi, Session session, SystemMetadata sysmeta)
    throws InvalidMetadataException {
    String doiName = doi.getDoiName();
    DataCiteMetadata dataCiteMetadata = new DataCiteMetadata();
    dataCiteMetadata.setTitles(DataCiteMetadata.Titles.builder()
                                 .withTitle(DataCiteMetadata.Titles.Title.builder().withValue(doiName).build())
                                 .build());
    dataCiteMetadata.setIdentifier(DataCiteMetadata.Identifier.builder()
                                     .withValue(doiName)
                                     .withIdentifierType(IdentifierType.DOI.name())
                                     .build());
    dataCiteMetadata.setCreators(extractCreators(session));
    dataCiteMetadata.setPublisher(session.getSubject().getValue());
    dataCiteMetadata.setPublicationYear(Integer.toString(LocalDate.now().getYear()));
    AlternateIdentifiers.Builder<?> altIds = AlternateIdentifiers.builder();
    toAlternateIdentifier(sysmeta.getObsoletedBy()).ifPresent(altIds::addAlternateIdentifier);
    toAlternateIdentifier(sysmeta.getObsoletes()).ifPresent(altIds::addAlternateIdentifier);
    dataCiteMetadata.setAlternateIdentifiers(altIds.build());
    return DataCiteValidator.toXml(doi, dataCiteMetadata);
  }

  /**
   * Full constructor.
   * @param dataRepository data repository implementation
   * @param doiRegistrationService registration service
   */
  public DataRepoBackend(DataRepository dataRepository, DoiRegistrationService doiRegistrationService,
                         DataRepoBackendConfiguration configuration) {
    this.dataRepository = dataRepository;
    this.doiRegistrationService = doiRegistrationService;
    this.configuration = configuration;
  }

  @Override
  public Checksum checksum(Identifier identifier, String checksumAlgorithm) {
    return dataRepository.getByAlternativeIdentifier(identifier.getValue())
          .map(DataRepoBackend::dataPackageChecksum)
          .orElseThrow(() -> new NotFound("Identifier Not Found", identifier.getValue()));
  }

  @Override
  public void close() {
    // NOP
  }


  @Override
  public Identifier create(Session session, Identifier pid, InputStream object, SystemMetadata sysmeta) {
    try {
      assertNotExists(pid);
      DataPackage dataPackage = new DataPackage();
      dataPackage.setCreated(new Date());
      AlternativeIdentifier alternativeIdentifier = new AlternativeIdentifier();
      alternativeIdentifier.setType(AlternativeIdentifier.Type.UNKNOWN);
      alternativeIdentifier.setIdentifier(pid.getValue());
      dataPackage.setCreatedBy(session.getSubject().getValue());
      dataPackage.setTitle(pid.getValue());
      dataPackage.addAlternativeIdentifier(alternativeIdentifier);
      //formatId is added as Tag to be later used during search
      Optional.ofNullable(sysmeta.getFormatId())
        .ifPresent(formatId -> dataPackage.addTag(toDataOneTag(formatId)));

      DOI doi = doiRegistrationService.generate(DoiType.DATA_PACKAGE);
      String metadataXML = toDataRepoMetadata(doi, session, sysmeta);
      doiRegistrationService.register(DoiRegistration.builder()
                                        .withType(DoiType.DATA_PACKAGE)
                                        .withDoi(doi)
                                        .withUser(session.getSubject().getValue())
                                        .withMetadata(metadataXML)
                                        .build());
      dataPackage.setDoi(doi);
      dataRepository.create(dataPackage, wrapInInputStream(metadataXML),
                            Lists.newArrayList(toFileContent(object), toFileContent(sysmeta)));
      return pid;
    } catch (InvalidMetadataException | JAXBException ex) {
      LOG.error("Error processing metadata", ex);
      throw new InvalidSystemMetadata("Error registering data package metadata");
    }
  }

  /**
   * Extracts the Creators information from the session object.
   */
  private static DataCiteMetadata.Creators extractCreators(Session session) {
    List<DataCiteMetadata.Creators.Creator> creators = Optional.ofNullable(session.getSubjectInfo())
                                                        .map(subjectInfo -> subjectInfo.getPerson().stream()
                                                          .map(person ->
                                                            DataCiteMetadata.Creators.Creator.builder()
                                                              .withCreatorName(person.getGivenName() + " "
                                                                               + person.getFamilyName()).build()
                                                       ).collect(Collectors.toList())).orElse(new ArrayList<>());
    Optional.ofNullable(session.getSubject())
      .ifPresent(subject -> creators.add(DataCiteMetadata.Creators.Creator.builder()
                                           .withCreatorName(subject.getValue()).build()));
    return DataCiteMetadata.Creators.builder().withCreator(creators).build();
  }

  @Override
  public Identifier delete(Session session, Identifier pid) {
    LOG.info("Deleting data package {}, session: {}", pid, session);
    return getAndConsume(pid, dataPackage -> {
                                                dataRepository.delete(dataPackage.getDoi());
                                                return pid;
                                              });
  }

  @Override
  public DescribeResponse describe(Identifier identifier) {
    return getAndConsume(identifier, dataPackage -> {
                                      SystemMetadata systemMetadata = systemMetadata(identifier);
                                      return new DescribeResponse(systemMetadata.getFormatId(),
                                                                  BigInteger.valueOf(dataPackage.getSize()),
                                                                  dataPackage.getModified(),
                                                                  dataPackageChecksum(dataPackage),
                                                                  systemMetadata.getSerialVersion());
                                      });
  }

  @Override
  public Identifier generateIdentifier(Session session, String scheme, String fragment) {
    return Identifier.builder().withValue(doiRegistrationService.generate(DoiType.DATA_PACKAGE).getDoiName()).build();
  }

  @Override
  public InputStream get(Identifier identifier) {
    return getAndConsume(identifier, dataPackage ->
            dataRepository.getFileInputStream(dataPackage.getDoi(), CONTENT_FILE)
          ).orElseThrow(() -> new NotFound("Content file not found for identifier", identifier.getValue()));
  }

  @Override
  public Health health() {
   return Health.healthy();
  }

  @Override
  public ObjectList listObjects(NodeReference self, Date fromDate, @Nullable Date toDate, @Nullable String formatId,
                                @Nullable Boolean replicaStatus, @Nullable Integer start, @Nullable Integer count) {
    PagingRequest pagingRequest = new PagingRequest(Optional.ofNullable(start).orElse(0),
                                                    Optional.ofNullable(count)
                                                      .map(value -> Integer.min(value, MAX_PAGE_SIZE))
                                                      .orElse(MAX_PAGE_SIZE));
    List<String> tags  = Optional.ofNullable(formatId)
                          .map(value -> Collections.singletonList(toDataOneTag(value))).orElse(null);
    PagingResponse<DataPackage> response = dataRepository.list(null, pagingRequest, fromDate, toDate, false, tags);
    return ObjectList.builder().withCount(response.getLimit())
      .withStart(Long.valueOf(response.getOffset()).intValue())
      .withTotal(response.getCount().intValue())
      .withObjectInfo(response.getResults().stream()
                        .map(dataPackage -> ObjectInfo.builder()
                                              .withIdentifier(Identifier.builder()
                                                                .withValue(dataPackage.getAlternativeIdentifiers().get(0).getIdentifier()).build())
                                              .withChecksum(dataPackageChecksum(dataPackage))
                                              .withDateSysMetadataModified(toXmlGregorianCalendar(dataPackage
                                                                                                    .getModified()))
                                              .withSize(BigInteger.valueOf(dataPackage.getSize()))
                                              .build())
                        .collect(Collectors.toList()))
      .build();
  }

  @Override
  public SystemMetadata systemMetadata(Identifier identifier) {
    return getAndConsume(identifier,  dataPackage ->
              dataRepository.getFileInputStream(dataPackage.getDoi(), SYS_METADATA_FILE)
                .map(file -> {
                  try {
                    SystemMetadata metadata = (SystemMetadata)JAXB_CONTEXT.createUnmarshaller().unmarshal(file);
                    if (metadata.getSerialVersion() == null) {
                      return metadata.newCopyBuilder().withSerialVersion(BigInteger.ONE).build();
                    }
                    return metadata;
                  } catch (JAXBException ex) {
                    LOG.error("Error reading XML system metadata", ex);
                    throw new InvalidSystemMetadata("Error reading system metadata");
                  }
                })
           ).orElseThrow(() -> new NotFound("Metadata Not Found for Identifier", identifier.getValue()));
  }

  /**
   * Validates that the system metadata is valid for a an update operation.
   */
  private static void validateUpdateMetadata(SystemMetadata sysmeta, Identifier pid) {
    if (Optional.ofNullable(sysmeta.getObsoletedBy()).map(Identifier::getValue).isPresent()) {
      throw new InvalidSystemMetadata("A new object cannot be created in an obsoleted state");
    }
    if (Optional.ofNullable(sysmeta.getObsoletes()).filter(obseletesId -> !obseletesId.equals(pid)).isPresent()) {
      throw new InvalidSystemMetadata("Obsoletes is set does not match the pid of the object being obsoleted");
    }
  }

  /**
   * Validates that the systemMetadata is not already being obsoleted.
   */
  private static void validateIsObsoleted(SystemMetadata systemMetadata) {
    if (Optional.ofNullable(systemMetadata.getObsoletedBy()).map(Identifier::getValue).isPresent()) {
      throw new InvalidSystemMetadata("ObsoletedBy is already set on the object being obsoleted");
    }
  }

  /**
   * Add the altPid identifier to the list of AlternateIdentifiers of the metadata associated to the DOI parameter.
   *
   */
  private String addAlternateIdentifiersMetadata(DOI doi, Identifier altPid) {
    try {
      DataCiteMetadata metadata =
        DataCiteValidator.fromXml(dataRepository.getFileInputStream(doi, DataPackage.METADATA_FILE).get());
      metadata.setAlternateIdentifiers(AlternateIdentifiers.copyOf(metadata.getAlternateIdentifiers())
                                         .addAlternateIdentifier(AlternateIdentifier.builder()
                                                                   .withValue(altPid.getValue())
                                                                   .withAlternateIdentifierType(IdentifierType.UNKNOWN
                                                                                                  .name())
                                                                   .build())
                                         .build());
      return DataCiteValidator.toXml(doi, metadata);
    } catch (JAXBException | InvalidMetadataException ex) {
      LOG.error("Error reading metadata", ex);
      throw new InvalidSystemMetadata("Error reading metadata");
    }
  }
  @Override
  public Identifier update(Session session, Identifier pid, InputStream object, Identifier newPid,
                           SystemMetadata sysmeta) {
    return getAndConsume(pid, dataPackage -> {
            validateUpdateMetadata(sysmeta, pid);
            assertNotExists(newPid);
            SystemMetadata obsoletedMetadata = getSystemMetadata(session, pid);
            validateIsObsoleted(obsoletedMetadata);
            XMLGregorianCalendar now = toXmlGregorianCalendar(new Date());
            dataRepository.update(dataPackage,
                                  wrapInInputStream(addAlternateIdentifiersMetadata(dataPackage.getDoi(), newPid)),
                                  Collections.singletonList(toFileContent(obsoletedMetadata.newCopyBuilder()
                                                                            .withObsoletedBy(newPid)
                                                                            .withDateSysMetadataModified(now)
                                                                            .build())),
                                  DataRepository.UpdateMode.APPEND);
            return create(session, newPid, object, sysmeta.newCopyBuilder()
                                                    .withDateSysMetadataModified(now)
                                                    .withObsoletes(pid)
                                                    .build());
        });

  }

  @Override
  public SystemMetadata getSystemMetadata(Session session, Identifier identifier) {
    return systemMetadata(identifier);
  }

  @Override
  public void archive(Identifier identifier) {
    getAndConsume(identifier, dataPackage -> { dataRepository.archive(dataPackage.getDoi()); return Void.TYPE;});
  }

  @Override
  public boolean isAuthorized(Session session, Identifier identifier, Permission action) {
    return getAndConsume(identifier, dataPackage ->
      (action == Permission.READ) ||
      ((action == Permission.WRITE || action == Permission.CHANGE_PERMISSION)
       && dataPackage.getCreatedBy().equals(session.getSubject().getValue()))
    );
  }

  @Override
  public long getEstimateCapacity() {
    return configuration.getStorageCapacity() - dataRepository.getStats().getTotalSize();
  }

  /**
   * Gets a data package and applies the mapper function to it.
   */
  private <T> T getAndConsume(Identifier identifier, Function<DataPackage,T> mapper) {
    return dataRepository.getByAlternativeIdentifier(identifier.getValue())
      .map(mapper::apply)
      .orElseThrow(() -> new NotFound("Identifier Not Found", identifier.getValue()));
  }

}
