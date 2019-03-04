console.log(Math.floor(6.5)) // arredonda para baixo

console.log(Math.ceil(6.4)) // arredonda para cima


function Obj(nome){
    this.nome = nome //this para ficar publico, se usar var ele fica somente no contexto da função
}

const obj2 = new Obj('Cadeira')
const obj3 = new Obj('Mesa')

console.log(obj2.nome)
console.log(obj3.nome)

