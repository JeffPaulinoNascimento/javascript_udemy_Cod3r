const alunos = [
    { nome: "Joao", nota: 5, bolsista: false },
    { nome: "Maria", nota: 10, bolsista: true },
    { nome: "Pedro", nota: 2, bolsista: false },
    { nome: "Ana", nota: 15, bolsista: true }
]

//Desafio 1: todos os alunos são bolsistas?
const saoBolsistas = (resultado, bolsista) => {  
    let result = resultado && bolsista;    
    return result;
};

console.log(`Todos os alunos são bolsistas? ${alunos.map(a => a.bolsista).reduce(saoBolsistas)? "Sim":"Não"}` );


//Desafio 2: Existe algum bolsista?
const algumBolsista = (resultado, bolsista) => (resultado || bolsista) ? "Sim" : "Não";
console.log(`Algum aluno é bolsista? ${alunos.map(a => a.bolsista).reduce(algumBolsista)}`);
