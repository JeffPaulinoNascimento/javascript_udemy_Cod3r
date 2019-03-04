// par nome / valor
const saudacao = 'Opa' //contexto léxico

function exec() {
    const saudacao = 'Falaaa' // contexto léxico 2 
    return saudacao
}

// Objetos são grupos aninhados de pares nome/valor

const cliente = {
    nome: 'Jefferson',
    idade: 26,
    peso: 32,
    endereco: {
        logradouro: 'Rua feliz',
        numero: 123,
        nome: 'outro'
    }
}

console.log(saudacao)
console.log(exec())
console.log(cliente.log)