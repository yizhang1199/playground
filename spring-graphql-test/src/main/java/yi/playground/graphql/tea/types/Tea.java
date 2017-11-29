package yi.playground.graphql.tea.types;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Tea {
    private final String id = UUID.randomUUID().toString();
    ;
    private TeaType type;
    private String name;
    private String description;
    private List<String> producedInPlaceIds;

    public Tea(String name, TeaType type, String description, List<String> producedInPlaceIds) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.producedInPlaceIds = Collections.unmodifiableList(producedInPlaceIds);
    }

    public Tea(TeaInput input) {
        this(input.getTeaName(), input.getType(), input.getDescription(), input.getProducedInPlaceIds());
    }

    public String getId() {
        return id;
    }

    public TeaType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getProducedInPlaceIds() {
        return producedInPlaceIds;
    }
}
