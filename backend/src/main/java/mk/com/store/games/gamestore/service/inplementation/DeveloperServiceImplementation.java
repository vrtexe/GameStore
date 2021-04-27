package mk.com.store.games.gamestore.service.inplementation;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import mk.com.store.games.gamestore.model.Developer;
import mk.com.store.games.gamestore.model.Publisher;
import mk.com.store.games.gamestore.model.User;
import mk.com.store.games.gamestore.model.dto.DeveloperDto;
import mk.com.store.games.gamestore.model.exception.DeveloperNotFoundException;
import mk.com.store.games.gamestore.model.exception.PublisherNotFoundException;
import mk.com.store.games.gamestore.model.exception.UserNotFoundException;
import mk.com.store.games.gamestore.repository.DeveloperRepository;
import mk.com.store.games.gamestore.repository.GameRepository;
import mk.com.store.games.gamestore.repository.PublisherRepository;
import mk.com.store.games.gamestore.repository.UserRepository;
import mk.com.store.games.gamestore.service.DeveloperService;

@Service
public class DeveloperServiceImplementation implements DeveloperService {

    private final UserRepository userRepository;
    private final PublisherRepository publisherRepository;
    private final DeveloperRepository developerRepository;
    private final GameRepository gameRepository;

    public DeveloperServiceImplementation(UserRepository userRepository, PublisherRepository publisherRepository,
            DeveloperRepository developerRepository, GameRepository gameRepository) {
        this.userRepository = userRepository;
        this.publisherRepository = publisherRepository;
        this.developerRepository = developerRepository;
        this.gameRepository = gameRepository;
    }

    
    @Override
    public List<Developer> getAllDevelopers() {
        return developerRepository.findAll();
    }

    @Override
    public List<Developer> getAllDevelopersForPublisher(String username, String publisherId) throws UserNotFoundException, PublisherNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);
        Publisher publisher = user.getPublishers().stream().filter(pub -> pub.getId().equals(publisherId)).findFirst().orElseThrow(PublisherNotFoundException::new);
        return publisher.getStudios();
    }

    @Override
    public Optional<Developer> addNewDeveloper(DeveloperDto developerDto) throws PublisherNotFoundException, UserNotFoundException {
        User user = userRepository.findByUsername(developerDto.getUsername()).orElseThrow(UserNotFoundException::new);
        Publisher publisher = user.getPublishers().stream().filter(pub -> pub.getId().equals(developerDto.getPublisherId())).findFirst().orElseThrow(PublisherNotFoundException::new);
        Developer developer = new Developer(developerDto.getName());
        developerRepository.save(developer);
        publisher.getStudios().add(developer);
        publisherRepository.save(publisher);
        return Optional.of(developer);
    }

    @Override
    public Optional<Developer> editDeveloper(String developerId, DeveloperDto developerDto) throws UserNotFoundException, PublisherNotFoundException, DeveloperNotFoundException {
        User user = userRepository.findByUsername(developerDto.getUsername()).orElseThrow(UserNotFoundException::new);
        Publisher publisher = user.getPublishers().stream().filter(pub -> pub.getId().equals(developerDto.getPublisherId())).findFirst().orElseThrow(PublisherNotFoundException::new);
        Developer developer = developerRepository.findById(developerId).orElseThrow(DeveloperNotFoundException::new);
        if(!publisher.getStudios().contains(developer)){
            Publisher currentPublisher = publisherRepository.findByStudios(developer).orElseThrow(PublisherNotFoundException::new);
            currentPublisher.getStudios().remove(developer);
            publisherRepository.save(currentPublisher);
            publisher.getStudios().add(developer);
            publisherRepository.save(publisher);
        }
        developer.setName(developerDto.getName());
        return Optional.of(developerRepository.save(developer));
    }

    @Override
    public Optional<Boolean> removeDeveloper(String developerId,DeveloperDto developerDto) throws DeveloperNotFoundException, UserNotFoundException, PublisherNotFoundException {
        User user = userRepository.findByUsername(developerDto.getUsername()).orElseThrow(UserNotFoundException::new);
        Publisher publisher = user.getPublishers().stream().filter(pub -> pub.getId().equals(developerDto.getPublisherId())).findFirst().orElseThrow(PublisherNotFoundException::new);
        Developer developer = publisher.getStudios().stream().filter(dev -> dev.getId().equals(developerId)).findFirst().orElseThrow(DeveloperNotFoundException::new);
        if(!gameRepository.findByDeveloper(developer).isEmpty()){
            return Optional.of(false);
        }
        publisher.getStudios().remove(developer);
        publisherRepository.save(publisher);
        developerRepository.delete(developer);
        return Optional.of(true);
    }
    
}