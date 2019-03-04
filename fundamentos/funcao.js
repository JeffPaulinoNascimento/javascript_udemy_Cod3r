//função sem retorno
function imprimirSoma(a, b){
    console.log(a + b)
}
imprimirSoma(2,3)
imprimirSoma(2)
imprimirSoma(2,10,2,3,5,8) // pega os dois primeiros e ignora o resto
imprimirSoma()

//função com retorno
function soma(a, b = 0){
    return a + b
}

console.log(soma(2,5))
console.log(soma(2))