const pilotos = ["Vettel", "Alonso", "Raikkonen", "Massa"]
pilotos.pop() // o pop exclui o ultimo elemento do array
console.log(pilotos);

pilotos.push("Verstappen");
console.log(pilotos);

pilotos.shift() //remove o primeiro elemento da lista
console.log(pilotos);

pilotos.unshift("Hamilton"); // adiciona um elemento no primeiro indice
console.log(pilotos);

pilotos.splice(2, 0, "Bottas", "Massa"); // A partir do segundo elemento, não remove ninguem e adiciona o Bottas e o Massa
console.log(pilotos);

pilotos.splice(3, 1)
console.log(pilotos);

const algunsPilotos1 = pilotos.slice(2) // novo array, slice é igual a pedaço, ele pega um pedaço do array a partir do indice 2 nesse exemplo
console.log(algunsPilotos1);

const algunsPilotos2 = pilotos.slice(1, 4) // pega um pedaço do array a partir do indice 1 até indice 4, ou seja os indices 1, 2 e 3
console.log(pilotos);
console.log(algunsPilotos2);