import {
  Box, Button,
  Card,
  CardHeader, Divider, Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow, Typography
} from '@mui/material';
import { Scrollbar } from '../../components/scrollbar';
import { SeverityPill } from '../../components/severity-pill';
import { convertToDate } from '../../helpers/helper-functions';
import { UserTransactions } from '../../helpers/helper-rest-functions';
import { GAME_TRANSACTIONS_REST_PATH, TRANSACTION_OUTCOME } from '../../helpers/helper-constants';
import RotateRightIcon from '@mui/icons-material/RotateRight';

export const OverviewLatestGameTransactions = () => {
  const { transactions, txMutator } = UserTransactions(GAME_TRANSACTIONS_REST_PATH);
  const updateTxs = async () => {
    await txMutator();
  };
  return (
    <Card sx={{ height: '100%' }}>
      <Stack direction="row"
             alignItems="center"
             justifyContent="space-between"
             margin="5px"
      >
        <CardHeader title="10 Game Transactions"/>
        <Button component="label"
                variant="contained"
                startIcon={<RotateRightIcon/>}
                onClick={updateTxs}
        >
          Sync
        </Button>
      </Stack>
      <Scrollbar sx={{ flexGrow: 1 }}>
        <Box sx={{ width: '100%' }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>
                  Id
                </TableCell>
                <TableCell>
                  Predicted Outcome
                </TableCell>
                <TableCell>
                  Win
                </TableCell>
                <TableCell>
                  Eth Amount
                </TableCell>
                <TableCell>
                  Profit
                </TableCell>
                <TableCell sortDirection="desc">
                  Created At
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {transactions.map((tx) => {

                return (
                  <TableRow
                    hover
                    key={tx.id}
                  >
                    <TableCell>
                      <Typography fontSize="small">
                        {tx.id}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      {tx.predictedOutcome}
                    </TableCell>
                    <TableCell>
                      {TRANSACTION_OUTCOME[tx.transactionOutcome]}
                    </TableCell>
                    <TableCell>
                      <SeverityPill>
                        {tx.ethAmount}
                      </SeverityPill>
                    </TableCell>
                    <TableCell>
                      <SeverityPill>
                        {tx.ethProfit}
                      </SeverityPill>

                    </TableCell>
                    <TableCell>
                      {convertToDate(tx.createdAt)}
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