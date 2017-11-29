package yi.playground.graphql.tea.types;

import graphql.schema.GraphQLInputType;

import java.util.Collections;
import java.util.List;

public class TeaInput implements GraphQLInputType {
    private TeaType type;
    private String name;
    private String description;
    private List<String> producedInPlaceIds;

    // Default constructor required for deserialization
    public TeaInput() {
    }

    public TeaInput(String name, TeaType type, String description, List<String> producedInPlaceIds) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.producedInPlaceIds = Collections.unmodifiableList(producedInPlaceIds);

    }

    public TeaType getType() {
        return type;
    }

    public String getTeaName() {
        return name;
    }

    public String getName() {
        return "TeaInput";
    }

    public String getDescription() {
        return description;
    }

    public List<String> getProducedInPlaceIds() {
        return producedInPlaceIds;
    }
}
