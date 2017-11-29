package yi.playground.graphql.tea.repository;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;
import yi.playground.graphql.tea.types.Place;
import yi.playground.graphql.tea.types.Tea;
import yi.playground.graphql.tea.types.TeaInput;
import yi.playground.graphql.tea.types.TeaType;

import java.util.*;

@Component
public class TeaRepository {
    private final List<Tea> teas = new ArrayList<>();
    private final Map<String, Place> places;

    public TeaRepository() {
        teas.add(new Tea("Gyokuro", TeaType.Green, "Deep green infusion with mellow, full-bodied sweet taste and seaweed flavor", Arrays.asList("1")));
        teas.add(new Tea("Silver Needle", TeaType.White, "Pale yellow infusion with sweet taste and vegetal notes", Arrays.asList("2")));
        teas.add(new Tea("DongDing", TeaType.Oolong, "Light yellow infusion with sweet taste and floral aroma", Arrays.asList("3")));
        teas.add(new Tea("Iron Goddess", TeaType.Oolong, "Bright gold infusion with sweet taste and orchid aroma", Arrays.asList("2")));

        Map<String, Place> places = new HashMap<>();
        places.put("1", new Place("1", "Japan"));
        places.put("2", new Place("2", "Fujian"));
        places.put("3", new Place("3", "Taiwan"));
        places.put("4", new Place("4", "India"));

        this.places = Collections.unmodifiableMap(places);
    }

    public Tea addTea(TeaInput input) {
        Validate.notNull(input);
        Tea tea = new Tea(input);
        this.teas.add(tea);
        return tea;
    }

    public List<Tea> getTeas() {
        return Collections.unmodifiableList(teas);
    }

    public Map<String, Place> getPlaces() {
        return places;
    }
}
