package binary.wz.im.registry.zookeeper;

import binary.wz.im.registry.NotifyListener;
import binary.wz.im.registry.RegistryService;
import binary.wz.im.registry.domain.RegistryConfig;
import binary.wz.im.registry.domain.ServiceConfig;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author binarywz
 * @date 2022/7/13 22:45
 * @description:
 */
public class ZookeeperRegistryService implements RegistryService {

    private final static Logger logger = LoggerFactory.getLogger(ZookeeperRegistryService.class);

    private final static String DEFAULT_ROOT = "/thor-im";

    private final static String PATH_SEPARATOR = "/";

    private final static Integer SERVICE_OFFLINE = 0;

    private final String SERVICE_PATH;

    private final String SERVICE_GROUP;

    private final CuratorFramework zkClient;

    private NotifyListener notifyListener;

    private ObjectMapper objectMapper;

    public ZookeeperRegistryService(RegistryConfig registryConfig, ServiceConfig serviceConfig) {
        try {
            this.SERVICE_GROUP = serviceConfig.getGroup();
            this.SERVICE_PATH = DEFAULT_ROOT + PATH_SEPARATOR + SERVICE_GROUP;
            this.objectMapper = new ObjectMapper();
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            this.zkClient = CuratorFrameworkFactory.builder()
                    .connectString(registryConfig.getAddress())
                    .retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 1000))
                    .connectionTimeoutMs(5000)
                    .build();
            zkClient.start();
            if (serviceConfig.getType() == 1 &&
                    zkClient.checkExists().forPath(DEFAULT_ROOT) == null) {
                zkClient.create().withMode(CreateMode.PERSISTENT).forPath(DEFAULT_ROOT);
            }
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Override
    public void register(String path, ServiceConfig config) {
        try {
            zkClient.create().creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(SERVICE_PATH+ PATH_SEPARATOR + path, objectMapper.writeValueAsBytes(config));
        } catch (Exception e) {
            logger.error("Failed to register {} to zookeeper, cause: {}", path, e.getMessage(), e);
        }
    }

    @Override
    public void unregister(String path) {
        try {
            zkClient.delete().guaranteed().forPath(SERVICE_PATH+ PATH_SEPARATOR + path);
        } catch (Exception e) {
            logger.error("Failed to delete {}, cause: {}", path, e.getMessage(), e);
        }
    }

    @Override
    public void subscribe(NotifyListener notifyListener) {
        try {
            this.notifyListener = notifyListener;
            PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient,SERVICE_PATH, true);
            pathChildrenCache.getListenable().addListener((client, event) -> {
                ChildData childData = event.getData();
                if (childData == null) return;
                ServiceConfig config = objectMapper.readValue(childData.getData(), ServiceConfig.class);
                if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                    config.setStatus(SERVICE_OFFLINE);
                }
                this.notifyListener.notify(config);
            });
            // pathChildrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
            pathChildrenCache.start();
        } catch (Exception e) {
            logger.error("Failed to subscribe, cause: {}", e.getMessage(), e);
        }
    }

    @Override
    public void unsubscribe() {

    }

    @Override
    public List<ServiceConfig> lookup() {
        try {
            List<String> serviceList = this.zkClient.getChildren().forPath(SERVICE_PATH);
            if (serviceList == null || serviceList.isEmpty()) return null;
            List<ServiceConfig> configs = new ArrayList<>();
            for (String serviceKey : serviceList) {
                byte[] bytes = this.zkClient.getData().forPath(SERVICE_PATH + PATH_SEPARATOR + serviceKey);
                configs.add(objectMapper.readValue(bytes, ServiceConfig.class));
            }
            return configs;
        } catch (Exception e) {
            logger.error("Failed to lookup service list, cause: {}", e.getMessage(), e);
        }
        return null;
    }
}
