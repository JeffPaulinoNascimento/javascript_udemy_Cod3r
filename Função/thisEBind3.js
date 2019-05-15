/*function Pessoa(){
    this.idade = 0;

    setInterval(function(){
        this.idade++;
        console.log(this.idade);
    }, 1000) // Nesse caso o this.idade não aponta para o objeto pessoa, pelo fato do this.idade++ não ser disparado pelo obejto pessoa e sim pela função
}*/


/*
function Pessoa(){
    this.idade = 0;

    setInterval(function(){
        this.idade++;
        console.log(this.idade);
    }.bind(this), 1000) // Nesse caso o bind(this) está apontando para o objeto pessoa
}
*/


// ou assim

function Pessoa(){
    this.idade = 0;
    const self = this;

    setInterval(function(){
        self.idade++;
        console.log(self.idade);
    }, 1000)
}

new Pessoa;