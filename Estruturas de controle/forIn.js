const notas = [6.4, 5.8, 8.9, 9.9, 2.5]

for (i in notas){
    console.log(i, notas[i]);
}

const pessoa = {
    nome: "Ana",
    sobrenome: "Silva",
    idade: 29,
    peso: 64
}

for (atributo in pessoa){
    console.log(`${atributo} = ${pessoa[atributo]}`)
}