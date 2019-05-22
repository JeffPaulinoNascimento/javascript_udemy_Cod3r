function criarProduto(nome, preco){
    return{
        nome: nome,
        preco: preco,
        desconto: 0.1
    }
}

console.log(criarProduto("Veja", 25))