package yi.playground.graphql.tea.resolvers;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import yi.playground.graphql.tea.repository.TeaRepository;
import yi.playground.graphql.tea.types.Tea;
import yi.playground.graphql.tea.types.TeaInput;

@Component
public class TeaMutationResolver implements GraphQLMutationResolver {

    @Autowired
    private TeaRepository teaRepository;

    public Tea addTea(TeaInput input) {
        return teaRepository.addTea(input);
    }
}