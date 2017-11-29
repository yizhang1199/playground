package yi.playground.graphql.tea.resolvers;

import com.coxautodev.graphql.tools.GraphQLResolver;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import yi.playground.graphql.tea.repository.TeaRepository;
import yi.playground.graphql.tea.types.Place;
import yi.playground.graphql.tea.types.Tea;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class PlaceResolver implements GraphQLResolver<Place> {
    @Autowired
    private TeaRepository teaRepository;

    public List<Tea> produces(Place place) {
        Validate.notNull(place);
        return teaRepository.getTeas().stream().filter(p -> p.getProducedInPlaceIds().contains(place.getId())).collect(Collectors.toList());
    }
}
