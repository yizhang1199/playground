# Test project 

**Documentation**
1. http://www.baeldung.com/spring-graphql
2. https://medium.com/graphql-mastery/graphql-quick-tip-how-to-pass-variables-into-a-mutation-in-graphiql-23ecff4add57

**Graphiql on localhost:**

http://localhost:9000/graphiql

`query exploreTeas {
  teas {
    id
    name
    type
    description
    producedIn {
      name
    }
  }
  
  places {
    id
    name
    produces {
      name
      type
      description
    }
  }
}

mutation addTeas {
  addBlackTea:addTea(input: {
    name:"Chai"
    type:Black
    description:"Best with milk"
    producedInPlaceIds:["4"]
  }) {
    id
    type
    name
    producedIn {
      name
    }
  }
  
  addAnotherTea:addTea(input: {
    name:"another green tea"
    type:Green
    description:"blah blah"
    producedInPlaceIds:["1", "2", "3"]
  }) {
    id
    type
    name
    producedIn {
      name
    }
  }
}`