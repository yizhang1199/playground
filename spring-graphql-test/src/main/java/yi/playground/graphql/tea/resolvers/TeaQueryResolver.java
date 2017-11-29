package yi.playground.graphql.tea.resolvers;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import yi.playground.graphql.tea.repository.TeaRepository;
import yi.playground.graphql.tea.types.Place;
import yi.playground.graphql.tea.types.Tea;
import yi.playground.graphql.tea.types.TeaType;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TeaQueryResolver implements GraphQLQueryResolver {

    @Autowired
    private TeaRepository teaRepository;

    public List<Tea> teas(TeaType type) {
        List<Tea> matchedTeas = teaRepository.getTeas().stream().filter(p -> (type == null || p.getType().equals(type))).collect(Collectors.toList());
        return matchedTeas;
    }

    public List<Place> places(String placeId) {
        return teaRepository.getPlaces().values().stream().filter(p -> (placeId == null || p.getId().equals(placeId))).collect(Collectors.toList());
    }
}