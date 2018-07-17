  mvn clean compile exec:java -Dexec.mainClass="com.datastax.banking.Main"  -DcontactPoints=localhost -Dcreate=true -DnoOfTransactions=1000000 -DnoOfCustomers=100000 -DnoOfDays=5
