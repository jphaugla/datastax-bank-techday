  mvn clean compile exec:java -Dexec.mainClass="com.datastax.banking.Main"  -DcontactPoints=dc0vm0 -Dcreate=true -DnoOfTransactions=1000000 -DnoOfCustomers=100000 -DnoOfDays=0
