const produtos = [
    {nome: "Notebook", preco: 2499, fragil: true },
    {nome: "Ipad Pro", preco: 4199, fragil: true },
    {nome: "Copo de Vidro", preco: 12.49, fragil: true },
    {nome: "Copo de Plastico", preco: 18.99, fragil: false }
]
/* retornar somente produtos com preço maior que 2500
console.log(produtos.filter(function(p){
    return p.preco > 2500;
}));
*/

// retornar somente produtos com preço maior que 2000 e frageis
console.log(produtos.filter(function(p){
    return p.preco > 2000 && p.fragil === true;
}));

const caro = produto => produto.preco > 500;
const fragil = produto => produto.fragil;

console.log(produtos.filter(caro).filter(fragil));



