const alunos = [
    { nome: "Joao", nota: 5},
    { nome: "Maria", nota: 10}
];

// Imperativo
let total1 = 0;

for (let i = 0; i < alunos.length; i++){
    total1 += alunos[i].nota; 
}
