function soma(){
    let soma = 0;
    for (i in arguments){ // arguments é um array interno de uma função que contem todos os elementos que foram passados para essa função, mesmo não estando entre parenteses os argumentos
        soma += arguments[i];
    }
    return soma;
}


console.log(soma());
console.log(soma(1));
console.log(soma(1.1, 2.2, 3.3));
console.log(soma(1.1, 2.2, "teste"));
console.log(soma('a', 'b', 'c'));

