package sharespace.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sharespace.model.OwnerDetails;
import sharespace.model.Room;
import sharespace.repository.RoomRepository;
import sharespace.service.OwnerServiceImpl;
import org.modelmapper.ModelMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerConfigurationFactoryBean;

import java.util.Arrays;
import java.util.List;

@Configuration
@Component
@EnableAspectJAutoProxy
@EnableScheduling
@EnableAsync
public class RoomConfig implements CommandLineRunner {

    private final OwnerServiceImpl ownerService;
    private final RoomRepository roomRepository;


    @Value("${spring.frontend.url}")
    private String frontendUrl;

    @Value("${owner.name}")
    private String ownername;

    @Value("${owner.password}")
    private String password;

    private static final Logger logger= LoggerFactory.getLogger(RoomConfig.class);

    public RoomConfig(OwnerServiceImpl ownerService, RoomRepository roomRepository) {
        this.ownerService = ownerService;
        this.roomRepository = roomRepository;
    }


    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns(frontendUrl)
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }

    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("SendingMail-");
        executor.initialize();
        return executor;
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Override
    public void run(String... args) throws Exception {
        if (roomRepository.count() == 0) {
            List<Room> roomList = Arrays.asList(
                    new Room(1, "F1", "Single Sharing", 1, 0, true, 8500.00, 250.00,null),
                    new Room(1, "F2", "Two Sharing", 2, 0, true, 7500.00, 230.00,null),
                    new Room(1, "F3", "Three Sharing", 3, 0, true, 6500.00, 200.00,null),
                    new Room(2, "S1", "Single Sharing", 1, 0, false, 8000.00, 230.00,null),
                    new Room(2, "S2", "Two Sharing", 2, 0, false, 7000.00, 210.00,null),
                    new Room(2, "S3", "Three Sharing", 3, 0, false, 6000.00,180.00, null)
            );
            roomRepository.saveAll(roomList);

            logger.info("Initial data inserted into the database.");
        } else {
            logger.info("Data already exists in the database. Skipping initialization.");
        }

        OwnerDetails ownerDetails=new OwnerDetails(ownername,password);
        ownerService.addOwnerDetails(ownerDetails);

        logger.info("Owner Details added Successfully");
    }

    @Primary
    @Bean
    public FreeMarkerConfigurationFactoryBean factoryBean() {
        FreeMarkerConfigurationFactoryBean factoryBean = new FreeMarkerConfigurationFactoryBean();
        factoryBean.setTemplateLoaderPath("classpath:/templates");
        return factoryBean;
    }

}
