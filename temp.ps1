jbmc com.marketplace.Main --classpath target/classes --unwind 3 --trace | Select-String -Context 0,5 "Violated property" >> .\jbmcfalse.txt

jbmc com.marketplace.OrderCartDriver --classpath target/classes --unwind 3 --trace | Select-String -Context 0,5 "Violated property" >> .\jbmcfalse.txt

jbmc com.marketplace.ProductDriver --classpath target/classes --unwind 3 --trace | Select-String -Context 0,5 "Violated property" >> .\jbmcfalse.txt

jbmc com.marketplace.StatisticsDriver --classpath target/classes --unwind 3 --trace | Select-String -Context 0,5 "Violated property" >> .\jbmcfalse.txt

jbmc com.marketplace.AuthAdminDriver --classpath target/classes --unwind 3 --trace | Select-String -Context 0,5 "Violated property" >> .\jbmcfalse.txt
