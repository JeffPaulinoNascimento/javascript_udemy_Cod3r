const fabricantes = ["Mercedes", "Audi", "BMW"];

function imprimir(nome, indice){
    console.log(`${indice + 1}. ${nome}`);
}

fabricantes.forEach(imprimir) //ele vai percorrer o array com o foreach e para cada nome que ele for achando ele chama de volta a funcção imprimir
//cada elemento que  o foreach encontra ele chama o callback passando o elemento e o indice
