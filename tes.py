data = [ "empty-sequence()", "item()", "item()?", "item()*",
        "item()+", "string", "string?", "string*", "string+",
        "number", "number?", "number*", "number+", "boolean",
        "boolean?", "boolean*", "boolean+", "node()", "node()?",
        "node()*", "node()+", "element()", "element()?", "element()*",
        "element()+", "element(name)", "element(name)?", "element(name)*",
        "element(name)+", "map(*)", "map(string, item())", "map(number, item())",
        "map(boolean, item())", "array(*)", "array(item())", "function(*)",
        "function() as integer", "function(T) as R", "function(T1, T2) as R" ]

import itertools

for combination in itertools.combinations(data, 2):
    print(f"isSubtype({combination[0]}, {combination[1]}) -> ")

for combination in itertools.combinations(data, 2):
    print(f"{combination[0]} instance of {combination[1]},")