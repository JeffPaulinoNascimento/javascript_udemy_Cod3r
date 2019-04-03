const pessoa = {
    saudacao: "bom dia",
    falar(){
        console.log(this.saudacao);
    }
}

pessoa.falar();