const prod1 = {
    nome: "...",
    preco: 50
}

//Factory simples
function criarPessoa(){
    return{
        nome: "Jefferson",
        idade: 26
    }
}

console.log(criarPessoa());