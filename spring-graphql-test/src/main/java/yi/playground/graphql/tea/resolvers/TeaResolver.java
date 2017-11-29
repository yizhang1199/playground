package yi.playground.graphql.tea.resolvers;

import com.coxautodev.graphql.tools.GraphQLResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import yi.playground.graphql.tea.repository.TeaRepository;
import yi.playground.graphql.tea.types.Place;
import yi.playground.graphql.tea.types.Tea;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TeaResolver implements GraphQLResolver<Tea> {
    @Autowired
    private TeaRepository teaRepository;

    public List<Place> producedIn(Tea tea) {
        List<String> placeIds = tea.getProducedInPlaceIds();
        List<Place> places = teaRepository.getPlaces().values().stream().filter(p -> placeIds.contains(p.getId())).collect(Collectors.toList());
        return places;
    }
}
