input<-mtcars
input$am <- as.factor(input$am)
levels(input$am) <-c("AT", "MT")

testResult <- t.test(input$mpg~input$am)
print(testResult$p.value)