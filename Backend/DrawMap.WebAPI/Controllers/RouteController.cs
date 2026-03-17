using DrawMap.Domain;
using DrawMap.WebAPI.Services;
using Microsoft.AspNetCore.Mvc;
using DomainRoute = DrawMap.Domain.Route;

namespace DrawMap.WebAPI.Controllers;

[ApiController]
[Route("api/[controller]")]
public class RouteController : ControllerBase
{
    private readonly IRouteService _routeService;

    public RouteController(IRouteService routeService)
    {
        _routeService = routeService;
    }

    [HttpPost]
    public async Task<ActionResult<DomainRoute>> AddRoute([FromBody] DomainRoute route, CancellationToken cancellationToken)
    {
        var created = await _routeService.AddRoute(route, cancellationToken);
        return CreatedAtAction(nameof(GetRoute), new { routeId = created.Id }, created);
    }

    [HttpGet("{routeId}")]
    public async Task<ActionResult<DomainRoute>> GetRoute(string routeId, CancellationToken cancellationToken)
    {
        var route = await _routeService.GetRoute(routeId, cancellationToken);
        if (route is null)
            return NotFound();

        return Ok(route);
    }

    [HttpDelete("{routeId}")]
    public async Task<ActionResult<bool>> DeleteRoute(string routeId, CancellationToken cancellationToken)
    {
        var result = await _routeService.DeleteRoute(routeId, cancellationToken);
        if (!result)
            return NotFound();

        return Ok(result);
    }

    [HttpPut("{routeId}")]
    public async Task<ActionResult<bool>> UpdateRoute(string routeId, [FromBody] DomainRoute route, CancellationToken cancellationToken)
    {
        var result = await _routeService.UpdateRoute(routeId, route, cancellationToken);
        if (!result)
            return NotFound();

        return Ok(result);
    }
}
