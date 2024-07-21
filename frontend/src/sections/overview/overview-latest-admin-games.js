import { getUserBets } from '../../helpers/helper-rest-functions';
import {
  Box, Button,
  Card,
  CardHeader,
  Divider, Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography
} from '@mui/material';
import { Scrollbar } from '../../components/scrollbar';
import { convertToDate, getBetsFromCache } from '../../helpers/helper-functions';
import { SeverityPill } from '../../components/severity-pill';
import RotateRightIcon from '@mui/icons-material/RotateRight';

export const OverviewLatestAdminGames = () => {
  const bets = getBetsFromCache()
  const updateGames = async () => {
     await getUserBets()
  }
  return (
    <Card sx={{ height: '100%' }}>
      <Stack direction="row"
             alignItems="center"
             justifyContent="space-between"
             margin="5px"
      >
        <CardHeader title="10 Latest Games"/>
        <Button component="label"
                variant="contained"
                startIcon={<RotateRightIcon/>}
                onClick={updateGames}
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
                  Title
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
              {bets.map((game) => {

                return (
                  <TableRow
                    hover
                    key={game.id}
                  >
                    <TableCell>
                      <Typography fontSize="small">
                        {game.id}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      {game.title}
                    </TableCell>
                    <TableCell>
                      <SeverityPill>
                        {game.ethProfit}
                      </SeverityPill>

                    </TableCell>
                    <TableCell>
                      {convertToDate(game.createdAt)}
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