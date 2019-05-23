let aprovados = ["Bia", "Jonatham", "Carlos"];
console.log(aprovados);

aprovados[3] = "Guilherme";
aprovados.push("Vitor");

console.log(aprovados);

aprovados[9] = "Thiago";
console.log(aprovados)
console.log(aprovados.length);
console.log(aprovados[8] === undefined);

for(let i = 0; i < aprovados.length; i++){
    if(aprovados[i] === undefined){
        aprovados[i] = "Vazio";
    }
}
console.log(aprovados);
aprovados.sort();
console.log(aprovados);

delete aprovados[1];
console.log(aprovados);

aprovados = ["Bia", "Carlos", "Ana"];
aprovados.splice(1, 2) //a partir do indice 1 (Carlos) exclua dois elementos no caso (carlos e ana);
console.log(aprovados);



aprovados = ["Bia", "Carlos", "Ana"];
aprovados.splice(1, 2, "Elemento1", "Elemento2") //a partir do indice 1 (Carlos) exclua dois elementos no caso (carlos e ana) e adicione Elemento 1 e Elemento 2;
console.log(aprovados);