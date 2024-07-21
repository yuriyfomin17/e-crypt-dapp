import PropTypes from 'prop-types';
import {
    Box, Button,
    Card,
    CardHeader,
    Divider, Stack,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow, Typography
} from '@mui/material';
import {Scrollbar} from '/src/components/scrollbar';
import {SeverityPill} from '/src/components/severity-pill';
import Link from 'next/link';
import {CRYPTO_TRANSACTIONS_REST_PATH, statusMap, typeMap} from '../../helpers/helper-constants';
import {convertToDate} from '../../helpers/helper-functions';
import {UserTransactions} from '../../helpers/helper-rest-functions';
import RotateRightIcon from '@mui/icons-material/RotateRight';

export const OverviewLatestTransactions = () => {
    const {transactions, txMutator} = UserTransactions(CRYPTO_TRANSACTIONS_REST_PATH);
    const updateTransactions = async () => {
        await txMutator();
    };
    return (
        <Card sx={{height: '100%'}}>
            <Stack direction="row"
                   alignItems="center"
                   justifyContent="space-between"
                   margin="5px"
            >
                <CardHeader title="10 Deposit/Withdraw Transactions"/>
                <Button component="label"
                        variant="contained"
                        startIcon={<RotateRightIcon/>}
                        onClick={updateTransactions}
                >
                    Sync
                </Button>
            </Stack>

            <Scrollbar sx={{flexGrow: 1}}>
                <Box sx={{width: '100%'}}>
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>
                                    TxID
                                </TableCell>
                                <TableCell>
                                    State
                                </TableCell>
                                <TableCell>
                                    Type
                                </TableCell>
                                <TableCell>
                                    Percentage processed
                                </TableCell>
                                <TableCell>
                                    Amount
                                </TableCell>
                                <TableCell sortDirection="desc">
                                    Created At
                                </TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {transactions.map((order) => {
                                const state = statusMap[order.state];
                                const type = typeMap[order.type];
                                const color = state === statusMap.PROCESSED
                                    ? 'success'
                                    : 'error';
                                const hashLength = order.hash ? order.hash.length : 0;
                                const substringHash = order.hash && hashLength > 0 ?
                                    order.hash.substring(0, 4) + '...' + order.hash.substring(hashLength - 4,
                                        hashLength) : '';
                                const ethAmount = order.ethAmount === "0" ? "Processing": order.ethAmount
                                return (
                                    <TableRow
                                        hover
                                        key={order.hash}
                                    >
                                        <TableCell>
                                            <Typography fontSize="small">
                                                <Link href={'https://moonbase.moonscan.io/tx/' + order.hash}
                                                      target="_blank"
                                                      rel="noopener"
                                                >
                                                    {substringHash}
                                                </Link>
                                            </Typography>
                                        </TableCell>
                                        <TableCell>
                                            <SeverityPill color={color}>
                                                {state}
                                            </SeverityPill>
                                        </TableCell>
                                        <TableCell>
                                            {type}
                                        </TableCell>
                                        <TableCell>
                                            {order.percentage}%
                                        </TableCell>
                                        <TableCell>
                                            {ethAmount}
                                        </TableCell>
                                        <TableCell>
                                            {convertToDate(order.createdAt)}
                                        </TableCell>
                                    </TableRow>
                                );
                            })}
                        </TableBody>
                    </Table>
                </Box>
            </Scrollbar>
            <Divider/>
        </Card>
    );
};

OverviewLatestTransactions.prototype = {
    orders: PropTypes.array,
    sx: PropTypes.object
};
