library('se.alipsa:xmlr')

printXml <- function(title) {
  doc2 <- parse.xmlstring("
    <table xmlns='http://www.w3.org/TR/html4/'>
        <tr>
            <td>Apples</td>
            <td>Bananas</td>
        </tr>
    </table>")
  print(paste0(title, ": ", doc2))
}