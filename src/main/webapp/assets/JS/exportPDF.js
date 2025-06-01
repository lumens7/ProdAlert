function exportPDF() {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF('landscape');
    const nomeUsuario = localStorage.getItem("nomeUsuario") || "Nome do Usuário";
    const empresaUsuario = localStorage.getItem("empresaUsuario") || "N/A"; 
    const dataHora = new Date().toLocaleString();
    doc.setFontSize(18);
    doc.text("Relatório de Produtos", 14, 20);
    doc.setFontSize(12);
    doc.text(`Emitido por: ${nomeUsuario}`, 14, 30);
    if (empresaUsuario && empresaUsuario !== "N/A") {
        doc.text(`Empresa: ${empresaUsuario}`, 14, 40);
    }
    doc.text(`Data e Hora da emissão: ${dataHora}`, 14, 50);
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

    const data = resultados.map(produto => [
        produto.nomeProduto,
        produto.descricaoProduto || "N/A",
        produto.codBarras,
        produto.dataContagem,
        produto.quantidadeContada,
        produto.dataValidade,
        produto.funcionario.nomeUser,
    ]);
    doc.autoTable({
        head: [["Nome do Produto", "Descrição", "Código de Barras", "Data Contagem", "Quantidade", "Data Validade", "Funcionário"]],
        body: data,
        startY: 60,
        theme: 'grid',
        styles: {
            fontSize: 8,
            cellPadding: 2,
        },
        headStyles: {
            fillColor: [200, 200, 200],
            textColor: [0, 0, 0],
            fontStyle: 'bold'
        },
        alternateRowStyles: {
            fillColor: [240, 240, 240]
        }
    });
    doc.save("relatorio_produtos.pdf");
}