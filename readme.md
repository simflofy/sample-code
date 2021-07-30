# Simflofy Sample Code

Simflofy is a data management platform that supports several enterprise-level operations. The three areas where Simflofy excels are

- **Migration** -- Move billions of documents between connected systems.
- **Federation** -- Federated search and federated document management allows you to view and interact with data in several disparate systems from a single location/API.
- **Analysis** -- View and Analyze your digital footprint.

This project gives developers working examples to build their own modules and extend the Simflofy platform to better suite business needs.

The project can be used as a template to create:

1. Connectors
2. Processors (Job Tasks)
3. Post-processors

### Connectors

Connectors read from or write data to repositories (filesystems, databases, ECMs). Each connector can support read operations, write operations, or both. 

### Processors

When moving content through Simflofy, processors are tools that execute isolated units of work on the content as it is being moved. Processors can be used to perform simple tasks like changing file names or output directories. They can also be leveraged for more complex tasks like integrating data from third party systems or performing image analysis using a third-party AI tool.

### Post-processors

Post-processors function the same way processors would, but the work triggers after the document has been written.

Each module will create a jar that can be deployed to your Simflofy server.