package binary.wz.im.registry.zookeeper;

import binary.wz.im.registry.RegistryService;
import binary.wz.im.registry.domain.RegistryConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author binarywz
 * @date 2022/7/13 22:45
 * @description:
 */
public class ZookeeperRegistry implements RegistryService {

    private final static Logger logger = LoggerFactory.getLogger(ZookeeperRegistry.class);

    private final static String DEFAULT_ROOT = "thor-im";

    private final static String PATH_SEPARATOR = "/";

    private final String serviceGroup;

    private final CuratorFramework zkClient;

    private ObjectMapper objectMapper;

    public ZookeeperRegistry(RegistryConfig registryConfig) {
        try {
            this.serviceGroup = registryConfig.getGroup();
            this.objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            this.zkClient = CuratorFrameworkFactory.builder()
                    .connectString(registryConfig.getRegistryAddress())
                    .retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 1000))
                    .connectionTimeoutMs(5000)
                    .namespace(DEFAULT_ROOT + PATH_SEPARATOR + serviceGroup)
                    .build();
            zkClient.start();
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void register(String path, RegistryConfig config) {
        try {
            zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(PATH_SEPARATOR + path, objectMapper.writeValueAsBytes(config));
        } catch (Exception e) {
            logger.error("Failed to register {} to zookeeper, cause: {} ", path, e.getMessage(), e);
        }
    }

    @Override
    public void unregister(String path) {

    }
}
