package org.gbif.d1.mn.backend.impl;

import org.gbif.api.model.common.DOI;
import org.gbif.api.model.common.paging.PagingRequest;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.vocabulary.IdentifierType;
import org.gbif.d1.mn.backend.Health;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.datarepo.api.DataRepository;
import org.gbif.datarepo.api.model.DataPackage;
import org.gbif.datarepo.api.model.FileInputContent;
import org.gbif.doi.metadata.datacite.DataCiteMetadata;
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
  private static final Logger LOG = LoggerFactory.getLogger(DataRepoBackend.class);

  private static final String CHECKSUM_ALGORITHM  = "MD5";
  private static final String CONTENT_FILE  = "content";
  private static final String SYS_METADATA_FILE  = "system_metadata.xml";
  private static final String FORMAT_ID = "data_package";
  private static final JAXBContext JAXB_CONTEXT = getSystemMetadataJaxbContext();

  private final DataRepository dataRepository;
  private final DoiRegistrationService doiRegistrationService;

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
    return Checksum.builder()
      .withValue(dataPackage.getChecksum())
      .withAlgorithm(CHECKSUM_ALGORITHM)
      .build();
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
      throw new ServiceFailure("Error reading data package date");
    }
  }

  private static FileInputContent toFileContent(SystemMetadata sysmeta) throws JAXBException {
    StringWriter xmlMetadata = new StringWriter();
    JAXB_CONTEXT.createMarshaller().marshal(sysmeta, xmlMetadata);
    return new FileInputContent(SYS_METADATA_FILE,
                                new ByteArrayInputStream(xmlMetadata.toString().getBytes(StandardCharsets.UTF_8)));
  }

  private static Optional<DataCiteMetadata.AlternateIdentifiers.AlternateIdentifier> toAlternateIdentifier(Identifier pid) {
    return Optional.ofNullable(pid).map( obsoleteIdentifier ->
                                                       DataCiteMetadata.AlternateIdentifiers.AlternateIdentifier
                                                         .builder()
                                                         .withValue(obsoleteIdentifier.getValue())
                                                         .withAlternateIdentifierType(IdentifierType.DOI.name())
                                                         .build()

    );
  }

  /**
   *  Asserts that an pid exist, throws IdentifierNotUnique otherwise.
   */
  private void assertNotExists(Identifier pid) {
    if (dataRepository.get(new DOI(pid.getValue())).isPresent()) {
      throw new IdentifierNotUnique("Identifier already exists", pid.getValue());
    }
  }

  /**
   * Full constructor.
   * @param dataRepository data repository implementation
   * @param doiRegistrationService registration service
   */
  public DataRepoBackend(DataRepository dataRepository, DoiRegistrationService doiRegistrationService) {
    this.dataRepository = dataRepository;
    this.doiRegistrationService = doiRegistrationService;
  }

  @Override
  public Checksum checksum(Identifier identifier, String checksumAlgorithm) {
    return dataRepository.get(new DOI(identifier.getValue()))
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
      dataPackage.setCreatedBy(session.getSubject().getValue());
      dataPackage.setTitle(pid.getValue());
      DataCiteMetadata dataCiteMetadata = new DataCiteMetadata();
      dataCiteMetadata.setTitles(DataCiteMetadata.Titles.builder()
                                   .withTitle(DataCiteMetadata.Titles.Title.builder().withValue(pid.getValue()).build())
                                   .build());
      dataCiteMetadata.setIdentifier(DataCiteMetadata.Identifier.builder()
                                       .withValue(pid.getValue())
                                       .withIdentifierType(IdentifierType.DOI.name())
                                       .build());
      dataCiteMetadata.setCreators(extractCreators(session));
      dataCiteMetadata.setPublisher(session.getSubject().getValue());
      dataCiteMetadata.setPublicationYear(Integer.toString(LocalDate.now().getYear()));
      DataCiteMetadata.AlternateIdentifiers.Builder altIds =  DataCiteMetadata.AlternateIdentifiers.builder();
      toAlternateIdentifier(sysmeta.getObsoletedBy()).ifPresent(altIds::addAlternateIdentifier);
      toAlternateIdentifier(sysmeta.getObsoletes()).ifPresent(altIds::addAlternateIdentifier);
      dataCiteMetadata.setAlternateIdentifiers(altIds.build());
      DOI doi = new DOI(pid.getValue());
      String metadataXML = DataCiteValidator.toXml(doi, dataCiteMetadata);
      doiRegistrationService.register(DoiRegistration.builder()
                                        .withDoi(new DOI(pid.getValue()))
                                        .withType(DoiType.DATA_PACKAGE)
                                        .withUser(session.getSubject().getValue())
                                        .withMetadata(metadataXML)
                                        .build());
      dataPackage.setDoi(doi);
      dataRepository.create(dataPackage, new ByteArrayInputStream(metadataXML.getBytes(StandardCharsets.UTF_8)),
                            Lists.newArrayList(new FileInputContent(CONTENT_FILE, object), toFileContent(sysmeta)));
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
    dataRepository.delete(new DOI(pid.getValue()));
    return pid;
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
    return dataRepository.getFileInputStream(new DOI(identifier.getValue()), CONTENT_FILE)
            .orElseThrow(() -> new NotFound("Identifier not found", identifier.getValue()));
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
    PagingResponse<DataPackage> response = dataRepository.list(null, pagingRequest, fromDate, toDate, false);
    return ObjectList.builder().withCount(response.getLimit())
      .withStart(Long.valueOf(response.getOffset()).intValue())
      .withTotal(response.getCount().intValue())
      .withObjectInfo(response.getResults().stream()
                        .map(dataPackage -> ObjectInfo.builder()
                                              .withIdentifier(Identifier.builder()
                                                                .withValue(dataPackage.getDoi().getDoiName()).build())
                                              .withChecksum(dataPackageChecksum(dataPackage))
                                              .withDateSysMetadataModified(toXmlGregorianCalendar(dataPackage.getModified()))
                                              .withSize(BigInteger.valueOf(dataPackage.getSize()))
                                              .build())
                        .collect(Collectors.toList())).build();
  }

  @Override
  public SystemMetadata systemMetadata(Identifier identifier) {
    return dataRepository.getFileInputStream(new DOI(identifier.getValue()), SYS_METADATA_FILE)
      .map(file -> {
                      try {
                        return (SystemMetadata)JAXB_CONTEXT.createUnmarshaller().unmarshal(file);
                      } catch (JAXBException ex) {
                       throw new RuntimeException(ex);
                      }
                    }).orElseThrow(() -> new NotFound("Metadata Not Found", identifier.getValue()));
  }

  @Override
  public Identifier update(Session session, Identifier pid, InputStream object, Identifier newPid,
                           SystemMetadata sysmeta) {
    return getAndConsume(pid, dataPackage -> {
      try {
        XMLGregorianCalendar now = toXmlGregorianCalendar(new Date());
        SystemMetadata obsoletedMetadata = getSystemMetadata(session, pid)
                                            .newCopyBuilder().withObsoletedBy(newPid)
                                            .withDateSysMetadataModified(now)
                                            .build();
        DataCiteMetadata dataCiteMetadata = DataCiteValidator.fromXml(dataRepository
                                                                        .getFileInputStream(dataPackage.getDoi(),
                                                                                            DataPackage.METADATA_FILE)
                                                                        .get());
        dataCiteMetadata.setAlternateIdentifiers(DataCiteMetadata.AlternateIdentifiers
                                                   .copyOf(dataCiteMetadata.getAlternateIdentifiers())
                                                   .addAlternateIdentifier(DataCiteMetadata.AlternateIdentifiers
                                                                             .AlternateIdentifier.builder()
                                                                             .withValue(newPid.getValue())
                                                                             .withAlternateIdentifierType(
                                                                               IdentifierType.DOI.name()).build())
                                                   .build());
        dataRepository.update(dataPackage, new ByteArrayInputStream(DataCiteValidator
                                                                      .toXml(dataPackage.getDoi(), dataCiteMetadata)
                                                                      .getBytes(StandardCharsets.UTF_8)),
                              Collections.singletonList(toFileContent(obsoletedMetadata)),
                              DataRepository.UpdateMode.APPEND);
        SystemMetadata newObsoletedMetadata = sysmeta.newCopyBuilder()
                                                .withDateSysMetadataModified(now)
                                                .withObsoletes(pid)
                                                .build();
        return create(session, newPid, object, newObsoletedMetadata);
      } catch (JAXBException | InvalidMetadataException ex) {
        throw new RuntimeException(ex);
      }
    });

  }

  @Override
  public SystemMetadata getSystemMetadata(Session session, Identifier identifier) {
    return systemMetadata(identifier);
  }

  @Override
  public void archive(Identifier identifier) {
    dataRepository.archive(new DOI(identifier.getValue()));
  }

  @Override
  public boolean isAuthorized(Session session, Identifier identifier, Permission action) {
    return getAndConsume(identifier, dataPackage ->
      (action == Permission.READ) ||
      ((action == Permission.WRITE || action == Permission.CHANGE_PERMISSION)
       && dataPackage.getCreatedBy().equals(session.getSubject().getValue()))
    );
  }

  /**
   * Gets a data package and applies the mapper function to it.
   */
  private <T> T getAndConsume(Identifier identifier, Function<DataPackage,T> mapper) {
    return dataRepository.get(new DOI(identifier.getValue()))
      .map(mapper::apply)
      .orElseThrow(() -> new NotFound("Identifier Not Found", identifier.getValue()));
  }

}
