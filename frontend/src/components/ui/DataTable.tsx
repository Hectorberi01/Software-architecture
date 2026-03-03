import { ReactNode } from 'react';

export interface Column<T> {
    header: string;
    accessor: keyof T | ((item: T) => ReactNode);
}

interface DataTableProps<T> {
    data: T[];
    columns: Column<T>[];
    keyExtractor: (item: T) => string | number;
    emptyMessage?: string;
}

export function DataTable<T>({
    data,
    columns,
    keyExtractor,
    emptyMessage = 'Aucune donnée disponible'
}: DataTableProps<T>) {
    if (!data || data.length === 0) {
        return <div className="data-table-empty">{emptyMessage}</div>;
    }

    return (
        <div className="table-container">
            <table className="data-table">
                <thead>
                    <tr>
                        {columns.map((col, index) => (
                            <th key={index}>{col.header}</th>
                        ))}
                    </tr>
                </thead>
                <tbody>
                    {data.map((item) => (
                        <tr key={keyExtractor(item)}>
                            {columns.map((col, colIndex) => (
                                <td key={colIndex}>
                                    {typeof col.accessor === 'function'
                                        ? col.accessor(item)
                                        : String(item[col.accessor])}
                                </td>
                            ))}
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}
