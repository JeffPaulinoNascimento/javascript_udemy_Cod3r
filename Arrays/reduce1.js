const alunos = [
    { nome: "Joao", nota: 5, bolsista: false },
    { nome: "Maria", nota: 10, bolsista: true },
    { nome: "Pedro", nota: 2, bolsista: false },
    { nome: "Ana", nota: 15, bolsista: true }
]

console.log(alunos.map(a => a.nota));
const resultado = alunos.map(a => a.nota).reduce(function(acumulador, atual){
    console.log(acumulador, atual);
    return acumulador + atual;
})


/*Ou assim passando o valor inicial no final do metodo
console.log(alunos.map(a => a.nota));
const resultado = alunos.map(a => a.nota).reduce(function(acumulador, atual){
    console.log(acumulador, atual);
    return acumulador + atual;
}, 0)
*/

console.log(resultado);
