# Kudu

> Kudu source connector

## Description

Used to read data from Kudu. Currently, only supports Query with Batch Mode.

 The tested kudu version is 1.11.1.

## Options

| name                     | type    | required | default value |
|--------------------------|---------|----------|---------------|
| kudu_master             | string  | yes      | -             |
| kudu_table               | string  | yes      | -             |
| columnsList               | string  | yes      | -             |

### kudu_master [string]

`kudu_master` The address of kudu master,such as '192.168.88.110:7051'.

### kudu_table [string]

`kudu_table` The name of kudu table..

### columnsList [string]

`columnsList` Specifies the column names of the table.

## Examples

```hocon
source {
   KuduSource {
      result_table_name = "studentlyh2"
      kudu_master = "192.168.88.110:7051"
      kudu_table = "studentlyh2"
      columnsList = "id,name,age,sex"
    }

}
```