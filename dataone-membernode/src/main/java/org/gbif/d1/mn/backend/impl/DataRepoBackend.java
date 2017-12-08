package org.gbif.d1.mn.backend.impl;

import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.paging.PagingRequest;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.vocabulary.IdentifierType;
import org.gbif.d1.mn.backend.Health;
import org.gbif.d1.mn.backend.MNBackend;
import org.gbif.datarepo.api.DataRepository;
import org.gbif.datarepo.api.model.DataPackage;
import org.gbif.datarepo.api.model.FileInputContent;
import org.gbif.registry.doi.DoiType;
import org.gbif.registry.doi.registration.DoiRegistrationService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
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
import org.dataone.ns.service.exceptions.NotAuthorized;
import org.dataone.ns.service.exceptions.NotFound;
import org.dataone.ns.service.exceptions.NotImplemented;
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
  public static final String DATA_ONE_TAG_PREFIX = "DataOne";

  private static final Logger LOG = LoggerFactory.getLogger(DataRepoBackend.class);

  private static final String CHECKSUM_ALGORITHM  = "MD5";
  private static final String CONTENT_FILE  = "content";
  private static final String SYS_METADATA_FILE  = "dataone_system_metadata.xml";
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
    return DATA_ONE_TAG_PREFIX + ':' + value;
  }

  /**
   * Converts a java.util.Date into a XMLGregorianCalendar.
   */
  private static XMLGregorianCalendar toXmlGregorianCalendar(Date date) {
    try {
      GregorianCalendar gregorianCalendar = new GregorianCalendar();
      gregorianCalendar.setTime(date);
      gregorianCalendar.setTimeZone(TimeZone.getTimeZone("UTC"));
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
      return FileInputContent.from(SYS_METADATA_FILE,
                                   wrapInInputStream(xmlMetadata.toString()));
    } catch (JAXBException ex) {
      LOG.error("Error reading xml metadata", ex);
      throw new InvalidSystemMetadata("Error reading system metadata");
    }
  }

  /**
   *  Transforms a object into a FileInputContent.
   */
  private static FileInputContent toFileContent(InputStream object) throws JAXBException {
    return FileInputContent.from(CONTENT_FILE, object);
  }


  /**
   *  Asserts that an pid exist, throws IdentifierNotUnique otherwise.
   */
  private void assertNotExists(Identifier pid) {
    if (dataRepository.getByAlternativeIdentifier(pid.getValue()).isPresent()) {
      throw new IdentifierNotUnique("Identifier already exists", pid.getValue());
    }
  }

  private static void assertIsAuthorized(Session session, DataPackage dataPackage) {
    if (!isAuthorized(session,dataPackage, Permission.WRITE)) {
      throw new NotAuthorized("Subject is not authorized to perform this action");
    }
  }

  /**
   * Wraps a String in a ByteArrayInputStream.
   */
  private static InputStream wrapInInputStream(String in) {
    return new ByteArrayInputStream(in.getBytes(StandardCharsets.UTF_8));
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
      org.gbif.datarepo.api.model.Identifier alternativeIdentifier = new org.gbif.datarepo.api.model.Identifier();
      alternativeIdentifier.setType(org.gbif.datarepo.api.model.Identifier.Type.URL);
      alternativeIdentifier.setIdentifier(pid.getValue());
      alternativeIdentifier.setRelationType(org.gbif.datarepo.api.model.Identifier.RelationType.IsAlternativeOf);
      dataPackage.setCreatedBy(session.getSubject().getValue());
      dataPackage.setTitle(pid.getValue());
      dataPackage.addRelatedIdentifier(alternativeIdentifier);
      Date creationDate = sysmeta.getDateSysMetadataModified().toGregorianCalendar().getTime();
      dataPackage.setCreated(creationDate);
      dataPackage.setModified(creationDate);
      dataPackage.addTag(DATA_ONE_TAG_PREFIX);
      //formatId is added as Tag to be later used during search
      Optional.ofNullable(sysmeta.getFormatId())
        .ifPresent(formatId -> dataPackage.addTag(toDataOneTag(formatId)));
      dataRepository.create(dataPackage, Lists.newArrayList(toFileContent(object), toFileContent(sysmeta)), false);
      return pid;
    } catch (JAXBException ex) {
      LOG.error("Error processing metadata", ex);
      throw new InvalidSystemMetadata("Error registering data package metadata");
    }
  }


  @Override
  public Identifier delete(Session session, Identifier pid) {
    LOG.info("Deleting data package {}, session: {}", pid, session);
    return getAndConsume(pid, dataPackage -> {
                                                dataRepository.delete(dataPackage.getKey());
                                                return pid;
                                              });
  }

  @Override
  public DescribeResponse describe(Identifier identifier) {
    return getAndConsume(identifier, dataPackage -> {
                                      SystemMetadata systemMetadata = systemMetadata(identifier);
                                      return new DescribeResponse(systemMetadata.getFormatId(),
                                                                  systemMetadata.getSize(),
                                                                  dataPackage.getModified(),
                                                                  systemMetadata.getChecksum(),
                                                                  systemMetadata.getSerialVersion());
                                      });
  }

  @Override
  public Identifier generateIdentifier(Session session, String scheme, String fragment) {
    if (IdentifierType.DOI.name().equalsIgnoreCase(scheme)) {
      return Identifier.builder().withValue(doiRegistrationService.generate(DoiType.DATA_PACKAGE).getDoiName()).build();
    }
    if (IdentifierType.UUID.name().equalsIgnoreCase(scheme)) {
      return Identifier.builder().withValue(UUID.randomUUID().toString()).build();
    }
    throw new NotImplemented("Identifier schema not supported");
  }

  @Override
  public InputStream get(Identifier identifier) {
    return getAndConsume(identifier, dataPackage ->
            dataRepository.getFileInputStream(dataPackage.getKey(), CONTENT_FILE)
          ).orElseThrow(() -> new NotFound("Content file not found for identifier", identifier.getValue()));
  }

  @Override
  public Health health() {
   return Health.healthy();
  }

  @Override
  public ObjectList listObjects(NodeReference self, Date fromDate, @Nullable Date toDate, @Nullable String formatId,
                                @Nullable Boolean replicaStatus, @Nullable Integer start, @Nullable Integer count) {
    Pageable pagingRequest = new PagingRequest(Optional.ofNullable(start).orElse(0),
                                               Optional.ofNullable(count)
                                                      .map(value -> Integer.min(value, MAX_PAGE_SIZE))
                                                      .orElse(MAX_PAGE_SIZE));
    List<String> tags  = new ArrayList<>();
    tags.add(DATA_ONE_TAG_PREFIX);
    Optional.ofNullable(formatId).ifPresent(tags::add);
    PagingResponse<DataPackage> response = dataRepository.list(null, pagingRequest, fromDate, toDate, false, tags, null);
    return ObjectList.builder().withCount(response.getLimit())
      .withStart(Long.valueOf(response.getOffset()).intValue())
      .withTotal(Optional.ofNullable(response.getCount()).orElse(0L).intValue())
      .withObjectInfo(response.getResults().stream()
                        .map(dataPackage -> {
                          PagingResponse<org.gbif.datarepo.api.model.Identifier> identifiers =
                            dataRepository.listIdentifiers(null, null, null, dataPackage.getKey(), null,
                                                         org.gbif.datarepo.api.model.Identifier.RelationType.IsAlternativeOf,
                                                         null);
                          SystemMetadata systemMetadata = systemMetadata(dataPackage.getKey());
                          return ObjectInfo.builder()
                          .withIdentifier(Identifier.builder()
                                            .withValue(identifiers.getResults().get(0).getIdentifier()).build())
                          .withChecksum(systemMetadata.getChecksum())
                          .withDateSysMetadataModified(toXmlGregorianCalendar(dataPackage
                                                                                .getModified()))
                          .withSize(systemMetadata.getSize())
                          .build();
                        })
                        .collect(Collectors.toList()))
      .build();
  }

  @Override
  public SystemMetadata systemMetadata(Identifier identifier) {
    return getAndConsume(identifier,  dataPackage -> Optional.ofNullable(systemMetadata(dataPackage.getKey())))
            .orElseThrow(() -> new NotFound("Metadata Not Found for Identifier", identifier.getValue()));
  }


  public SystemMetadata systemMetadata(UUID dataPackageKey) {
    return  dataRepository.getFileInputStream(dataPackageKey, SYS_METADATA_FILE)
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
              }).orElseThrow(() -> new NotFound("Metadata Not Found", dataPackageKey.toString()));
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

  @Override
  public Identifier update(Session session, Identifier pid, InputStream object, Identifier newPid,
                           SystemMetadata sysmeta) {
    return getAndConsume(pid, dataPackage -> {
            validateUpdateMetadata(sysmeta, pid);
            assertNotExists(newPid);
            assertIsAuthorized(session, dataPackage);
            SystemMetadata obsoletedMetadata = getSystemMetadata(session, pid);
            validateIsObsoleted(obsoletedMetadata);
            Date dateNow = new Date();
            XMLGregorianCalendar now = toXmlGregorianCalendar(dateNow);
            if (dataPackage.getDeleted() != null) {
              throw new NotFound("Deleted objects can't be updated", pid.getValue());
            }
            dataPackage.setModified(dateNow);
            dataRepository.update(dataPackage,
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
  public boolean updateMetadata(Session session, Identifier pid, SystemMetadata sysmeta) {
    return getAndConsume(pid, dataPackage -> {
        validateUpdateMetadata(sysmeta, pid);
        assertIsAuthorized(session, dataPackage);
        if (dataPackage.getDeleted() != null) {
          throw new NotFound("Deleted objects can't be updated", pid.getValue());
        }
        dataRepository.update(dataPackage,
                              Collections.singletonList(toFileContent(sysmeta.newCopyBuilder()
                                                                        .withDateSysMetadataModified(
                                                                          toXmlGregorianCalendar(new Date()))
                                                                        .build())),
                              DataRepository.UpdateMode.APPEND);
        return Boolean.TRUE;
    });

  }

  @Override
  public SystemMetadata getSystemMetadata(Session session, Identifier identifier) {
    return systemMetadata(identifier);
  }

  @Override
  public void archive(Session session, Identifier identifier) {
    getAndConsume(identifier, dataPackage -> {
        assertIsAuthorized(session, dataPackage);
        SystemMetadata metadata = systemMetadata(dataPackage.getKey()).newCopyBuilder()
                                                  .withArchived(Boolean.TRUE)
                                                  .withDateSysMetadataModified(toXmlGregorianCalendar(new Date())).build();
        dataRepository.update(dataPackage,
                              Collections.singletonList(toFileContent(metadata)),
                              DataRepository.UpdateMode.APPEND);
        dataRepository.archive(dataPackage.getKey());
        return Void.TYPE;
    });
  }

  private static boolean isAuthorized(Session session, DataPackage dataPackage, Permission action) {
    return (action == Permission.READ) ||
           ((action == Permission.WRITE || action == Permission.CHANGE_PERMISSION)
            && dataPackage.getCreatedBy().equals(session.getSubject().getValue()));
  }

  @Override
  public long getEstimateCapacity() {
    return configuration.getStorageCapacity() - dataRepository.getStats().getTotalSize();
  }

  /**
   * Gets a data package and applies the mapper function to it.
   */
  private <T> T getAndConsume(Identifier identifier, Function<DataPackage,T> mapper) {
    //Only DataPackages tagged as 'DataOne' are shared throw this service
    return dataRepository.getByAlternativeIdentifier(identifier.getValue())
      .filter(dataPackage -> dataPackage.getTags().stream().anyMatch(tag -> DATA_ONE_TAG_PREFIX.equals(tag.getValue())))
      .map(mapper::apply)
      .orElseThrow(() -> new NotFound("Identifier Not Found", identifier.getValue()));
  }

}
