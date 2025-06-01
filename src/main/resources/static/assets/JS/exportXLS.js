function exportXLS() {
    // Dados da tabela
    const resultadosString = localStorage.getItem("resultadosPesquisa");
    let resultados = [];

    try {
        if (resultadosString && resultadosString !== "undefined") {
            resultados = JSON.parse(resultadosString);
        } else {
            showError("Nenhum dado disponível para exportar.", "", 5000, false);
            return;
        }
    } catch (error) {
        console.error("Erro ao analisar resultados da pesquisa:", error);
        showError("Erro ao carregar os resultados da pesquisa.", "", 5000, false);
        return;
    }

    if (!resultados || resultados.length === 0) {
        showError("Nenhum dado disponível para exportar.", "", 5000, false);
        return;
    }

    const nomeUsuario = localStorage.getItem("nomeUsuario") || "Nome do Usuário"; // Usa o nome do usuário do localStorage
    const empresaUsuario = localStorage.getItem("empresaUsuario") || "N/A"; // Usa o nome da empresa do localStorage
    const dataHora = new Date().toLocaleString();

    const cabecalhoPersonalizado = [
        ["Relatório de Produtos"],
        [`Emitido por: ${nomeUsuario}`],
        empresaUsuario && empresaUsuario !== "N/A" ? [`Empresa: ${empresaUsuario}`] : [],
        [`Data e Hora: ${dataHora}`],
        [] // Linha em branco para separar o cabeçalho da tabela
    ];

    // Cria um array com os dados da tabela
    const data = resultados.map(produto => [
        produto.nomeProduto,
        produto.descricaoProduto || "N/A",
        produto.codBarras,
        produto.dataContagem,
        produto.quantidadeContada,
        produto.dataValidade,
        produto.funcionario.nomeUser,
    ]);

    // Cabeçalho da tabela
    const header = [
        "Nome do Produto",
        "Descrição",
        "Código de Barras",
        "Data de Contagem",
        "Quantidade Contada",
        "Data de Validade",
        "Funcionário",
    ];

    // Combina o cabeçalho personalizado com os dados da tabela
    const fullData = [...cabecalhoPersonalizado, header, ...data];

    // Cria uma planilha
    const ws = XLSX.utils.aoa_to_sheet(fullData);

    // Aplica estilos ao cabeçalho personalizado
    const cabecalhoStyle = {
        font: { bold: true, sz: 14 },
        alignment: { horizontal: "left" }
    };

    for (let i = 0; i < cabecalhoPersonalizado.length; i++) {
        const cellAddress = XLSX.utils.encode_cell({ r: i, c: 0 });
        if (!ws[cellAddress]) continue;
        ws[cellAddress].s = cabecalhoStyle;
    }

    // Aplica estilos ao cabeçalho da tabela
    const headerStyle = {
        font: { bold: true, color: { rgb: "FFFFFF" } },
        fill: { fgColor: { rgb: "4F81BD" } }, // Cor de fundo azul
        alignment: { horizontal: "center" }
    };

    const range = XLSX.utils.decode_range(ws['!ref']);
    for (let C = range.s.c; C <= range.e.c; ++C) {
        const cellAddress = XLSX.utils.encode_cell({ r: cabecalhoPersonalizado.length, c: C }); // Cabeçalho da tabela
        if (!ws[cellAddress]) continue;
        ws[cellAddress].s = headerStyle;
    }

    // Aplica estilos às linhas alternadas
    for (let R = cabecalhoPersonalizado.length + 1; R <= range.e.r; ++R) {
        for (let C = range.s.c; C <= range.e.c; ++C) {
            const cellAddress = XLSX.utils.encode_cell({ r: R, c: C });
            if (!ws[cellAddress]) continue;

            // Alterna cores das linhas
            ws[cellAddress].s = {
                fill: { fgColor: { rgb: R % 2 === 0 ? "FFFFFF" : "F2F2F2" } } // Linhas alternadas
            };
        }
    }

    // Cria um novo workbook e adiciona a planilha
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Relatório de Produtos");

    // Gera o arquivo Excel
    XLSX.writeFile(wb, "relatorio_produtos.xlsx");
}