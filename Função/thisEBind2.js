function cook(){
    console.log(this.ingredients);
}

let dinner  = {
    ingredients:"bacon"
};

const cookBoundToDinner = cook.bind(dinner);
cookBoundToDinner();