const notas = [7.7, 6.5, 5.2, 8.9, 3.6, 7.1, 9.0];

/*

// Retornar as notas menores do que 7
// resolvido por mim

function menorQueSete(numeros){
    for(let i = 0; i < numeros.length; i++){
        if(numeros[i] < 7){
            console.log(`Essa nota: ${numeros[i]}, é menor que sete`);
        }
    }
}

menorQueSete(notas);
*/

// resolução do prof

let notasBaixas = [];
for(let i in notas){
    if(notas[i] < 7){
        notasBaixas.push(notas[i]);
    }
}

console.log(notasBaixas);


const notasBaixas2 = notas.filter(function(nota){
    return nota < 7
}) // o filter percorre o array e passa uma nota para a função validar se é menor que 7, se sim, ele adiciona essa nota na variavel notasBaixas2

console.log(notasBaixas2);

const notasBaixa3 = notas.filter( nota => nota < 7);
console.log(notasBaixa3);