// estrategia 1 para gerar valor padrão
function soma1(a, b, c){
    a = a || 1; // ele retorna quem for verdadeiro, se a for verdadeiro ele nem pega o numero 1;
    b = b || 1;
    c = c || 1;
    return a + b + c;
}

console.log(soma1(), soma1(3), soma1(1,2,3), soma1())
console.log(soma1(0,0,0));/*nesse caso ele vai assumir o valor false e vai pegar o 1*/

console.log("-----------------------------------------------------------------------")
//Estrategia 2, 3 e 4 para gerar valor padrão
function soma2(a, b, c){
    a = a !== undefined ? a : 1; // se 'a' for undefined ou seja não foi passado o valor de 'a' pega o numero 1
    b = 1 in arguments ? b : 1; //valida se existe o indice 1 dentro de arguments, ou seja tem o b
    c = isNaN(c) ? 1 : c //valida se o valor passado em 'c' é um numero, se não for um numero seta o numero 1 se não seta 'c'
    return a + b + c;
}

console.log(soma2());
console.log(soma2(3));
console.log(soma2(1,2,3));
console.log(soma2(0,0,0));

console.log("----------------------------------------------------");

//valor padrão do es2015 ecmaScript
function soma3(a = 1, b = 1, c = 1){
    return a + b + c;
}

console.log(soma3(), soma3(3), soma3(1,2,3), soma3(0,0,0))
