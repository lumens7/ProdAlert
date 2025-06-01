function exportXLS() {
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
    const nomeUsuario = localStorage.getItem("nomeUsuario") || "Nome do Usuário";
    const empresaUsuario = localStorage.getItem("empresaUsuario") || "N/A"; 
    const dataHora = new Date().toLocaleString();
    const cabecalhoPersonalizado = [
        ["Relatório de Produtos"],
        [`Emitido por: ${nomeUsuario}`],
        empresaUsuario && empresaUsuario !== "N/A" ? [`Empresa: ${empresaUsuario}`] : [],
        [`Data e Hora: ${dataHora}`],
        []
    ];
    const data = resultados.map(produto => [
        produto.nomeProduto,
        produto.descricaoProduto || "N/A",
        produto.codBarras,
        produto.dataContagem,
        produto.quantidadeContada,
        produto.dataValidade,
        produto.funcionario.nomeUser,
    ]);
    const header = [
        "Nome do Produto",
        "Descrição",
        "Código de Barras",
        "Data de Contagem",
        "Quantidade Contada",
        "Data de Validade",
        "Funcionário",
    ];
    const fullData = [...cabecalhoPersonalizado, header, ...data];
    const ws = XLSX.utils.aoa_to_sheet(fullData);
    const cabecalhoStyle = {
        font: { bold: true, sz: 14 },
        alignment: { horizontal: "left" }
    };
    for (let i = 0; i < cabecalhoPersonalizado.length; i++) {
        const cellAddress = XLSX.utils.encode_cell({ r: i, c: 0 });
        if (!ws[cellAddress]) continue;
        ws[cellAddress].s = cabecalhoStyle;
    }
    const headerStyle = {
        font: { bold: true, color: { rgb: "FFFFFF" } },
        fill: { fgColor: { rgb: "4F81BD" } }, 
        alignment: { horizontal: "center" }
    };
    const range = XLSX.utils.decode_range(ws['!ref']);
    for (let C = range.s.c; C <= range.e.c; ++C) {
        const cellAddress = XLSX.utils.encode_cell({ r: cabecalhoPersonalizado.length, c: C }); 
        if (!ws[cellAddress]) continue;
        ws[cellAddress].s = headerStyle;
    }
    for (let R = cabecalhoPersonalizado.length + 1; R <= range.e.r; ++R) {
        for (let C = range.s.c; C <= range.e.c; ++C) {
            const cellAddress = XLSX.utils.encode_cell({ r: R, c: C });
            if (!ws[cellAddress]) continue;
            ws[cellAddress].s = {
                fill: { fgColor: { rgb: R % 2 === 0 ? "FFFFFF" : "F2F2F2" } } 
            };
        }
    }
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, "Relatório de Produtos");
    XLSX.writeFile(wb, "relatorio_produtos.xlsx");
}