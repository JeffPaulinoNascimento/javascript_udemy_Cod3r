const alunos = [
    {
        nome: 'Joao',
        nota: 9.2, 
        bolsista: false
    },
    {
        nome: 'Maria',
        nota: 8.5, 
        bolsista: true
    },
    {
        nome: 'Gabriel',
        nota: 7.1, 
        bolsista: false
    },
    {
        nome: 'Diogo',
        nota: 6.8, 
        bolsista: true
    }
]
//desafio 1: todos os alunos são bolsistas

const todosBolsistas = (resultado, bolsista) => resultado && bolsista
console.log(alunos.map(a => a.bolsista).reduce(todosBolsistas))
