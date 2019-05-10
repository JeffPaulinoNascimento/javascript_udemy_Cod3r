let contador = 1;
while(contador <= 10){
    console.log(`O contador vale ${contador}`);
    contador++;
}

console.log("-----------Dentro do For----------------");
contador = 1;
for(let i = 1; i <= 10; i++){
    console.log(`O contador vale ${contador}`);
    contador++;
}

const notas = [6.4, 5.8, 8.9, 9.9, 2.5]
for(let i = 0; i < notas.length; i++){
    console.log(`notas = ${notas[i]}`);
}