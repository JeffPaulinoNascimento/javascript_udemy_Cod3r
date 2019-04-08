const express = require("express");
const app = express();
const bodyParser = require("body-parser")

app.use(bodyParser.urlencoded({ extended: true }))

    app.post("/usuarios", (req, resp) => {
        console.log(req.body);
        resp.send("<h1>Parabens. Usuário incluido com sucesso!</h1>");
    })

    app.post("/usuarios/:id", (req, resp) => {
        console.log(req.params.id);
        console.log(req.body);
        resp.send("<h1>Parabens. Usuário alterado com sucesso!</h1>");
    })

    app.listen(3003);