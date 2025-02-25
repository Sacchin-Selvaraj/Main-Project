package sharespace.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@EnableScheduling
public class RoomConfig implements CommandLineRunner {

    private final OwnerServiceImpl ownerService;

    private final RoomRepository roomRepository;

    private static final Logger logger= LoggerFactory.getLogger(RoomConfig.class);

    public RoomConfig(OwnerServiceImpl ownerService, RoomRepository roomRepository) {
        this.ownerService = ownerService;
        this.roomRepository = roomRepository;
    }

    @Bean
    public ModelMapper modelMapper() {

        return new ModelMapper();
    }

    @Override
    public void run(String... args) throws Exception {
        if (roomRepository.count() == 0) {
            List<Room> roomList = Arrays.asList(
                    new Room(1, "F1", "Single Sharing", 1, 0, true, 8500.00, null),
                    new Room(1, "F2", "Two Sharing", 2, 0, true, 7500.00, null),
                    new Room(1, "F3", "Three Sharing", 3, 0, true, 6500.00, null),
                    new Room(2, "S1", "Single Sharing", 1, 0, false, 8000.00, null),
                    new Room(2, "S2", "Two Sharing", 2, 0, false, 7000.00, null),
                    new Room(2, "S3", "Three Sharing", 3, 0, false, 6000.00, null)
            );
            roomRepository.saveAll(roomList);

            logger.info("Initial data inserted into the database.");
        } else {
            logger.info("Data already exists in the database. Skipping initialization.");
        }

        OwnerDetails ownerDetails=new OwnerDetails("Sacchin","1234");
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
