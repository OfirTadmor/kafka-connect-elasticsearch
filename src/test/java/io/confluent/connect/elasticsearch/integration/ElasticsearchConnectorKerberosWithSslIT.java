package io.confluent.connect.elasticsearch.integration;

import static io.confluent.connect.elasticsearch.ElasticsearchSinkConnectorConfig.CONNECTION_URL_CONFIG;
import static io.confluent.connect.elasticsearch.ElasticsearchSinkConnectorConfig.SECURITY_PROTOCOL_CONFIG;
import static io.confluent.connect.elasticsearch.ElasticsearchSinkConnectorConfig.SSL_CONFIG_PREFIX;

import io.confluent.connect.elasticsearch.ElasticsearchSinkConnectorConfig;
import io.confluent.connect.elasticsearch.ElasticsearchSinkConnectorConfig.SecurityProtocol;
import io.confluent.connect.elasticsearch.helper.ElasticsearchContainer;
import io.confluent.connect.elasticsearch.helper.ElasticsearchHelperClient;
import org.apache.kafka.common.config.SslConfigs;
import org.junit.BeforeClass;
import org.junit.Test;

public class ElasticsearchConnectorKerberosWithSslIT extends ElasticsearchConnectorKerberosIT{

  @BeforeClass
  public static void setupBeforeAll() throws Exception {
    initKdc();

    container = ElasticsearchContainer
        .fromSystemProperties()
        .withKerberosEnabled(esKeytab)
        .withSslEnabled(true);
    container.start();
  }

  @Override
  @Test
  public void testKerberos() {
    // skip as parent is running this
    helperClient = null;
  }

  @Test
  public void testKerberosWithSsl() throws Exception {
    // Use IP address here because that's what the certificates allow
    String address = container.getConnectionUrl().replace(
        container.getContainerIpAddress(),
        container.hostMachineIpAddress()
    );

    props.put(CONNECTION_URL_CONFIG, address);
    props.put(SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name());
    props.put(SSL_CONFIG_PREFIX + SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, container.getKeystorePath());
    props.put(SSL_CONFIG_PREFIX + SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, container.getKeystorePassword());
    props.put(SSL_CONFIG_PREFIX + SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, container.getTruststorePath());
    props.put(SSL_CONFIG_PREFIX + SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, container.getTruststorePassword());
    props.put(SSL_CONFIG_PREFIX + SslConfigs.SSL_KEY_PASSWORD_CONFIG, container.getKeyPassword());
    addKerberosConfigs(props);

    helperClient = new ElasticsearchHelperClient(
        address,
        new ElasticsearchSinkConnectorConfig(props)
    );

    // Start connector
    runSimpleTest(props);
  }
}
