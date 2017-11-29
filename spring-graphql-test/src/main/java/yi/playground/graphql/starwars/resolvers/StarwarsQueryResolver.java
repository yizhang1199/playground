package yi.playground.graphql.starwars.resolvers;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import yi.playground.graphql.starwars.repository.CharacterRepository;
import yi.playground.graphql.starwars.types.types.Character;
import yi.playground.graphql.starwars.types.types.Droid;
import yi.playground.graphql.starwars.types.types.Episode;
import yi.playground.graphql.starwars.types.types.Human;

@Component
public class StarwarsQueryResolver implements GraphQLQueryResolver {

    @Autowired
    private CharacterRepository characterRepository;

    public Character hero(Episode episode) {
        return episode != null ? characterRepository.getHeroes().get(episode) : characterRepository.getCharacters().get("1000");
    }

    public Human human(String id, DataFetchingEnvironment env) {
        return (Human) characterRepository.getCharacters().values().stream()
                .filter(character -> character instanceof Human && character.getId().equals(id))
                .findFirst()
                .orElseGet(null);
    }

    public Droid droid(String id) {
        return (Droid) characterRepository.getCharacters().values().stream()
                .filter(character -> character instanceof Droid && character.getId().equals(id))
                .findFirst()
                .orElseGet(null);
    }

    public Character character(String id) {
        return characterRepository.getCharacters().get(id);
    }
}

